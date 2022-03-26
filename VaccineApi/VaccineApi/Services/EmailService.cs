using MailKit.Net.Smtp;
using Microsoft.AspNetCore.Identity;
using Microsoft.Extensions.Options;
using MimeKit;
using MimeKit.Text;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using VaccineApi.Config;
using VaccineApi.Data;

namespace VaccineApi.Services
{
    public class EmailService : IEmailService
    {
        private const string templatePath = @"EmailTemplate/{0}.html";

        private readonly UserManager<ApplicationUser> _userManager;
        private readonly MailKitConfig _mailKitConfig;

        public EmailService(UserManager<ApplicationUser> userManager, IOptionsSnapshot<MailKitConfig> mailKitConfig)
        {
            _userManager = userManager;
            _mailKitConfig = mailKitConfig.Value;
        }

        public async Task SendForgotPasswordEmailAsync(ApplicationUser user, string path)
        {
            // Generate Password Reset Token -------------------------------------------------------------------------------
            string token = await _userManager.GeneratePasswordResetTokenAsync(user);

            // Update the placeholder in email template --------------------------------------------------------------------
            var keyValuePairs = new List<KeyValuePair<string, string>>()
            {
                new KeyValuePair<string, string>("{{name}}", user.Name),
                new KeyValuePair<string, string>("{{url}}", $"{path}?token={token}&uid={user.Id}")
            };

            // Get the html body -------------------------------------------------------------------------------------------
            string html = GetHTMLBody("ForgotPassword", keyValuePairs);

            // Send Email --------------------------------------------------------------------------------------------------
            await SendEmailAsync(html, "Forgot Password", user);
        }

        public async Task SendEmailConfirmationAsync(ApplicationUser user, string path)
        {
            // Generate Password Reset Token -------------------------------------------------------------------------------
            string token = await _userManager.GenerateEmailConfirmationTokenAsync(user);

            // Update the placeholder in email template --------------------------------------------------------------------
            var keyValuePairs = new List<KeyValuePair<string, string>>()
            {
                new KeyValuePair<string, string>("{{name}}", user.Name),
                new KeyValuePair<string, string>("{{url}}", $"{path}?token={token}&uid={user.Id}")
            };

            // Get the html body -------------------------------------------------------------------------------------------
            string html = GetHTMLBody("SignUp", keyValuePairs);

            // Send Email --------------------------------------------------------------------------------------------------
            await SendEmailAsync(html, "Account Signup", user);
        }

        private string GetHTMLBody(string htmlPage, List<KeyValuePair<string, string>> keyValuePairs)
        {
            // Get the forgot password email body ------------------------------------------------------
            string emailTemplate = string.Format(templatePath, htmlPage);
            string html = System.IO.File.ReadAllText(emailTemplate);

            // replace html text with key value pair ---------------------------------------------------
            if (!string.IsNullOrEmpty(html) && keyValuePairs != null)
            {
                foreach (var placeholder in keyValuePairs)
                {
                    if (html.Contains(placeholder.Key))
                    {
                        html = html.Replace(placeholder.Key, placeholder.Value);
                    }
                }
            }
            return html;
        }

        private async Task SendEmailAsync(string html, string subject, ApplicationUser user)
        {
            // Set From and To Email ---------------------------------------------------------------------------------------
            MimeMessage message = new MimeMessage();

            MailboxAddress from = new MailboxAddress(_mailKitConfig.SenderName, _mailKitConfig.SenderEmail);
            message.From.Add(from);

            MailboxAddress to = new MailboxAddress(user.Name, user.UserName);
            message.To.Add(to);

            // Set Subject -------------------------------------------------------------------------------------------------
            message.Subject = subject;

            // Put the message into email body -----------------------------------------------------------------------------
            message.Body = new TextPart(TextFormat.Html) { Text = html };

            // Send email using smtp client --------------------------------------------------------------------------------
            using (SmtpClient smtp = new SmtpClient())
            {
                await smtp.ConnectAsync(_mailKitConfig.Server, _mailKitConfig.Port, _mailKitConfig.RequireSSL);
                await smtp.AuthenticateAsync(_mailKitConfig.Account, _mailKitConfig.Password);
                await smtp.SendAsync(message);
                await smtp.DisconnectAsync(true);
            }
        }
    }
}
