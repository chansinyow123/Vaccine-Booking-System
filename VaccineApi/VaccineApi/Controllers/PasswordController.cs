using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using VaccineApi.Data;
using VaccineApi.Models.Password;
using VaccineApi.Services;

namespace VaccineApi.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class PasswordController : ControllerBase
    {
        private readonly UserManager<ApplicationUser> _userManager;
        private readonly IEmailService _emailService;

        public PasswordController(UserManager<ApplicationUser> userManager, IEmailService emailService)
        {
            _userManager = userManager;
            _emailService = emailService;
        }

        [HttpPost("forgot-password")]
        public async Task<IActionResult> ForgotPassword([FromBody] ForgotPasswordModel model)
        {
            // Check if username exist in database ----------------------------------------------------------------
            var user = await _userManager.FindByNameAsync(model.Username);
            if (user == null) return NotFound();
           
            // Send Forgot Password Email -------------------------------------------------------------------------
            await _emailService.SendForgotPasswordEmailAsync(user, model.DeepLink);

            return Ok();
        }

        [HttpPut("reset-password")]
        public async Task<IActionResult> ResetPassword([FromBody] ResetPasswordModel model)
        {
            // Check if user id exist in database ----------------------------------------------------------------
            var user = await _userManager.FindByIdAsync(model.UserId);
            if (user == null) return NotFound();

            // Check if reset password successfully --------------------------------------------------------------
            var result = await _userManager.ResetPasswordAsync(user, model.Token, model.Password);
            if (!result.Succeeded)
            {
                // loop through error and put into modelstate
                foreach (var e in result.Errors)
                    if (e.Code.StartsWith("Password"))
                        ModelState.AddModelError(nameof(model.Password), e.Description);
                    else
                        return NotFound();

                return ValidationProblem(ModelState);
            }

            return Ok();
        }

        [HttpPut("change-password")]
        [Authorize]
        public async Task<IActionResult> ChangePassword([FromBody] ChangePasswordModel model)
        {
            // Get the logged in user from database ----------------------------------------------------------------
            var username = User.Identity.Name;
            var user = await _userManager.FindByNameAsync(username);

            var result = await _userManager.ChangePasswordAsync(user, model.OldPassword, model.NewPassword);
            if (!result.Succeeded)
            {
                // loop through error and put into modelstate
                foreach (var e in result.Errors)
                    if (e.Code == "PasswordMismatch")
                        ModelState.AddModelError(nameof(model.OldPassword), e.Description);
                    else
                        ModelState.AddModelError(nameof(model.NewPassword), e.Description);

                return ValidationProblem(ModelState);
            }

            return Ok();
        }
    }
}
