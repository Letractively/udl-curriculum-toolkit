package org.cast.isi.page;

import net.databinder.hib.Databinder;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.cast.cwm.data.User;
import org.cast.cwm.data.component.FeedbackBorder;
import org.cast.cwm.service.EmailService;
import org.cast.cwm.service.UserService;
import org.cast.isi.ISIApplication;
import org.cast.isi.service.ISIEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForgotPassword extends ISIBasePage implements IHeaderContributor {
	

	boolean success = false; // has a successful send already happened?
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ForgotPassword.class);

	public ForgotPassword(PageParameters params) {
		super(params);

		String pageTitleEnd = (new StringResourceModel("ForgotPassword.pageTitle", this, null, "Forgot Password").getString());
		setPageTitle(pageTitleEnd);
		add(new Label("pageTitle", new PropertyModel<String>(this, "pageTitle")));
		add(new Label("applicationTitle", new StringResourceModel("applicationTitle", this, null)));
		add(new Label("applicationSubTitle", new StringResourceModel("applicationSubTitle", this, null)));
		
		add (new FeedbackPanel("feedback") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() { return anyMessage(); }
		});
		add (new EmailForm("form"));
		add(ISIApplication.get().getFooterPanel("pageFooter", params));
	}

	protected class EmailForm extends Form<Void> {

		TextField<String> email;

		private static final long serialVersionUID = 1L;
		
		protected EmailForm(String id) {
			super(id);

			add(new FeedbackBorder("emailBorder")
				.add(email = (TextField<String>) new TextField<String>("email", new Model<String>(""))
					.add(EmailAddressValidator.getInstance())
					.add(StringValidator.maximumLength(255))
					.setRequired(true)));
			add (new BookmarkablePageLink<Void>("register", Register.class));
			add (new BookmarkablePageLink<Void>("login", Login.class));			
		}

		@Override
		public boolean isVisible() {
			return !success;
		}

		@Override
		protected void onSubmit() {
			IModel<User> userModel = UserService.get().getByEmail(email.getModelObject());
			User user = userModel.getObject();
			if (user != null) {
				Databinder.getHibernateSession().beginTransaction();
				user.generateSecurityToken();
				Databinder.getHibernateSession().getTransaction().commit();
				String url = "/password?username=" + user.getUsername() + "&key=" + user.getSecurityToken();
				((ISIEmailService) EmailService.get()).sendXmlEmail(userModel, ISIEmailService.EMAIL_FORGOT, url);
				String passwordSent = new StringResourceModel("ForgotPassword.passwordSent", this, null,
						"Thank you! In a few minutes you should get an email.  You will need to click on the link in that email to reset your password.").getString();
				info(passwordSent);
				success = true;
			} else {
				String passwordNotFound = new StringResourceModel("ForgotPassword.passwordNotFound", this, null,
						"Sorry, there is no account with that email address.").getString();
				info(passwordNotFound);
			}
		}
	
	}

	public void renderHead(final IHeaderResponse response) {
		response.renderCSSReference(new ResourceReference("/css/main.css"));
		super.renderHead(response);		
	}

	@Override
	public String getPageType() {
		return null;
	}

	@Override
	public String getPageName() {
		return "forgotPassword";
	}

	@Override
	public String getPageViewDetail() {
		return null;
	}

}
