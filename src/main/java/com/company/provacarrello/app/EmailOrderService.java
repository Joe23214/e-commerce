package com.company.provacarrello.app;

import com.company.provacarrello.view.checkout.steps.PaymentStep;
import com.company.provacarrello.view.checkout.steps.ShippingStep;
import com.company.provacarrello.app.CarrelloService;
import io.jmix.email.EmailException;
import io.jmix.email.EmailInfo;
import io.jmix.email.EmailInfoBuilder;
import io.jmix.email.Emailer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

@Component
public class EmailOrderService {

    @Autowired
    private Emailer emailer;

    public void inviaEmailConfermaOrdine(String destinatario,
                                         CarrelloService carrelloService,
                                         ShippingStep shippingStep,
                                         PaymentStep paymentStep) throws EmailException {

        String subject = "Conferma Ordine - ProvaCarrello";
        String body = generaCorpoEmail(carrelloService, shippingStep, paymentStep);

        EmailInfo emailInfo = EmailInfoBuilder.create()
                .setAddresses(destinatario)
                .setSubject(subject)
                .setFrom("noreply@provacarrello.com")
                .setBody(body)
                .setBodyContentType(EmailInfo.HTML_CONTENT_TYPE)
                .build();

        emailer.sendEmail(emailInfo);
    }



    private String generaCorpoEmail(CarrelloService carrelloService,
                                    ShippingStep shippingStep,
                                    PaymentStep paymentStep) {
        NumberFormat euro = NumberFormat.getCurrencyInstance(Locale.ITALY);
        Map<com.company.provacarrello.entity.Prodotto, Integer> prodottiQuantita = carrelloService.getProdottiQuantita();

        StringBuilder sb = new StringBuilder();

        // Inizio HTML base con stile semplice
        sb.append("<html><body style='font-family: Arial, sans-serif; color: #333; margin: 20px;'>");

        sb.append("<h2 style='color: #007BFF; margin-bottom: 20px;'>Conferma Ordine - ProvaCarrello</h2>");
        sb.append("<h3 style='margin-bottom: 15px;'>Prodotti acquistati</h3>");

        // Card container con gap e margini
        sb.append("<div style='display: flex; flex-wrap: wrap; gap: 20px; margin-bottom: 40px;'>");

        for (Map.Entry<com.company.provacarrello.entity.Prodotto, Integer> entry : prodottiQuantita.entrySet()) {
            var p = entry.getKey();
            int qty = entry.getValue();

            double prezzoProdotto;
            try {
                prezzoProdotto = Double.parseDouble(p.getPrezzo().replace(",", "."));
            } catch (NumberFormatException e) {
                prezzoProdotto = 0;
            }
            double totaleProdotto = prezzoProdotto * qty;

            // Card singolo prodotto
            sb.append("<div style='border: 1px solid #ccc; border-radius: 8px; width: 220px; padding: 15px; box-shadow: 1px 1px 6px rgba(0,0,0,0.1); background-color: #fafafa;'>");

            // Immagine prodotto - usa https e placeholder se null
            String imgUrl = (p.getImg() != null && !p.getImg().toString().isEmpty())
                    ? p.getImg().toString()
                    : "https://via.placeholder.com/220x150?text=No+Image";

            sb.append("<img src='").append(imgUrl)
                    .append("' alt='").append(p.getNome())
                    .append("' style='width: 100%; height: 150px; object-fit: contain; border-radius: 5px; margin-bottom: 10px;'>");

            sb.append("<h4 style='margin: 0 0 10px 0; font-size: 1.1em;'>").append(p.getNome()).append("</h4>");
            sb.append("<p style='margin: 5px 0;'>Quantità: ").append(qty).append("</p>");
            sb.append("<p style='margin: 5px 0;'>Prezzo unitario: ").append(euro.format(prezzoProdotto)).append("</p>");
            sb.append("<p style='margin: 10px 0 0 0; font-weight: bold;'>Totale: ").append(euro.format(totaleProdotto)).append("</p>");

            sb.append("</div>");
        }
        sb.append("</div>"); // chiudo container prodotti

        // Spedizione
        double sub = prodottiQuantita.entrySet().stream()
                .mapToDouble(e -> {
                    try {
                        return Double.parseDouble(e.getKey().getPrezzo().replace(",", ".")) * e.getValue();
                    } catch (NumberFormatException ex) {
                        return 0.0;
                    }
                })
                .sum();

        double ship = shippingStep.getSelectedDelivery() == ShippingStep.Delivery.EXPRESS ? 42 : 0;

        sb.append("<h3 style='margin-bottom: 10px;'>Dettagli Spedizione</h3>")
                .append("<p><b>Consegna:</b> ").append(shippingStep.getSelectedDelivery()).append("</p>")
                .append("<p><b>Intestatario:</b> ").append(safeText(shippingStep.getEmail())).append("</p>")
                .append("<p><b>Indirizzo:</b> ").append(safeText(shippingStep.getAddress())).append("</p>")
                .append("<p><b>Città:</b> ").append(safeText(shippingStep.getCity()))
                .append(" (").append(safeText(shippingStep.getState())).append(")</p>")
                .append("<p><b>CAP:</b> ").append(safeText(shippingStep.getZip())).append("</p>")
                .append("<p><b>Telefono:</b> ").append(safeText(shippingStep.getPhone())).append("</p>");

        // Pagamento
        String cardNumber = paymentStep.getCardNumber().replaceAll("\\s+", "");
        String maskedCard = cardNumber.isEmpty()
                ? "-"
                : "**** **** **** " + cardNumber.substring(Math.max(0, cardNumber.length() - 4));

        sb.append("<h3 style='margin-top: 30px; margin-bottom: 10px;'>Dettagli Pagamento</h3>")
                .append("<p><b>Metodo:</b> ").append(safeText(paymentStep.getSelectedPayment().toString())).append("</p>")
                .append("<p><b>Intestatario carta:</b> ").append(safeText(paymentStep.getCardHolder())).append("</p>")
                .append("<p><b>Numero carta:</b> ").append(maskedCard).append("</p>")
                .append("<p><b>Scadenza:</b> ").append(safeText(paymentStep.getSelectedMonth()))
                .append("/").append(safeText(paymentStep.getSelectedYear())).append("</p>");

        // Totale ordine
        sb.append("<h3 style='margin-top: 30px;'>Riepilogo Totale</h3>")
                .append("<p><b>Subtotale:</b> ").append(euro.format(sub)).append("</p>")
                .append("<p><b>Spedizione:</b> ").append(ship == 0 ? "Gratis" : euro.format(ship)).append("</p>")
                .append("<p style='font-size: 1.2em; font-weight: bold;'>Totale: ").append(euro.format(sub + ship)).append("</p>");

        // Aggiunta scritta in fondo
        sb.append("<p style='font-size: 0.8em; color: #777; margin-top: 40px;'>");
        sb.append("Se non hai effettuato questo ordine, <a href='https://example.com/report-fraud' style='color: #007BFF; text-decoration: none;'>clicca qui</a> per segnalarlo.");
        sb.append("</p>");

        sb.append("</body></html>");

        return sb.toString();
    }




    private String safeText(String text) {
        return (text == null || text.isEmpty()) ? "-" : text;
    }
}
