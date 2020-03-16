package gr.dcu.europeana.arch.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "setting")
@Data
public class Setting {

    @Id
    private String key;
    private String value;
    private String description;

    public static final String MAILGUN_RECIPIENTS_SIGN_UP        = "mail.recipients.sign_up";
    public static final String MAILGUN_RECIPIENTS_RESET_PASSWORD = "mail.recipients.reset_password";
}
