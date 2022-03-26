using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Models.Password
{
    public class ForgotPasswordModel
    {
        [EmailAddress]
        [Required]
        public string Username { get; set; }

        [Required]
        [Url(ErrorMessage = "Invalid deep link url format.")]
        public string DeepLink { get; set; }
    }
}
