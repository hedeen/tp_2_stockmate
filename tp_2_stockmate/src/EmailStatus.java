import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import java.io.File;
import java.io.FileFilter;
import java.util.Properties;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailStatus {

	String[] tickerList;
	final String workingDir;
	final String username = "MrStockMate@gmail.com";
	final String password = "cha11engeeverything";

	public EmailStatus(String[] tickers, String dir) {
		this.tickerList = tickers;
		this.workingDir = dir;

	}
	
	public static void main(String[] args) {

	}

	public void GeneratePDFs() {

		Process p;

		try {
			for (int i = 0; i < tickerList.length; i++) {
				System.out.println("Generating R charts for: " + tickerList[i]);
				p = Runtime.getRuntime().exec("Rscript " + workingDir + "SM.R " + tickerList[i]);
				p.waitFor();
				p.destroy();
			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void SendSummaryEmail(String emailBody) {
		Date todayDate = Calendar.getInstance().getTime();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String todayString = formatter.format(todayDate);
		
		// setting gmail smtp properties
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		// check the authentication
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));

			// recipients email address
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("dave.hedeen@gmail.com"));

			// add the Subject of email
			message.setSubject("StockMATE Results");

			Multipart multipart = new MimeMultipart();

			// add the body message
			BodyPart bodyPart = new MimeBodyPart();
			bodyPart.setText(emailBody);
			multipart.addBodyPart(bodyPart);

			// attach the file

			try {
				File dir = new File(workingDir);
				FileFilter fileFilter = new WildcardFileFilter("*" + todayString + "*.pdf");
				File[] files = dir.listFiles(fileFilter);
				for (int i = 0; i < files.length; i++) {
					MimeBodyPart mimeBodyPart = new MimeBodyPart();
					mimeBodyPart.attachFile(files[i].getAbsolutePath());
					multipart.addBodyPart(mimeBodyPart);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			message.setContent(multipart);
			Transport.send(message);
			System.out.println("Email Sent Successfully");

		} catch (MessagingException e) {
			System.out.println("Email Failed");
		}

	}



	public String GetHTMLTable(ResultSet rst) {

		String emailBody = "";
		try {

			ResultSetMetaData metadata = rst.getMetaData();
			int columnCount = metadata.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				emailBody = emailBody + metadata.getColumnName(i) + "\t\t";
			}
			emailBody = emailBody + System.lineSeparator();
			while (rst.next()) {
				String row = "";
				for (int i = 1; i <= columnCount; i++) {
					row += rst.getString(i) + "\t\t";
				}
				emailBody = emailBody + row + System.lineSeparator();

			}

		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}

		return emailBody;
	}


}
