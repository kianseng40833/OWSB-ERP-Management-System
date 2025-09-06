package Management.Model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class Payment {
    private String paymentId;
    private String po_id;
    private String pr_id;
    private LocalDate date;
    private String status;
    private String totalAmount;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Payment(String paymentId, String po_id, String pr_id, String dateStr, String status, String totalAmount) {
        this.paymentId = paymentId;
        this.po_id = po_id;
        this.pr_id = pr_id;
        try {
            this.date = LocalDate.parse(dateStr, FORMATTER);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format: " + dateStr);
            this.date = null; // or LocalDate.now()
        }
        this.status = status;
        this.totalAmount = totalAmount;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getPo_id() {
        return po_id;
    }

    public String getPr_id() {
        return pr_id;
    }

    public String getStatus() {
        return status;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getDate() {
        return date != null ? date.format(FORMATTER) : "";
    }

    public void setDate(String dateStr) {
        this.date = LocalDate.parse(dateStr, FORMATTER);
    }

    public String toFileString() {
        return String.join(",", paymentId, po_id, pr_id, getDate(), status, totalAmount);
    }

    public static Payment fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length != 6) {
            System.err.println("Invalid data format: " + line);
            return null;
        }

        String dateStr = parts[3].trim();
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format in file: " + dateStr);
            return null;
        }

        return new Payment(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                dateStr,
                parts[4].trim(),
                parts[5].trim()
        );
    }
}