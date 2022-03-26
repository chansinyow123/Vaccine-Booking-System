using System.Threading.Tasks;
using VaccineApi.Data;

namespace VaccineApi.Services
{
    public interface IEmailService
    {
        Task SendForgotPasswordEmailAsync(ApplicationUser user, string path);
        Task SendEmailConfirmationAsync(ApplicationUser user, string path);
    }
}