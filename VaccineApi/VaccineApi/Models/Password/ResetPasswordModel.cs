using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Models.Password
{
    public class ResetPasswordModel
    {
        [Required]
        public string UserId { get; set; }
        [Required]
        public string Token { get; set; }
        [Required]
        public string Password { get; set; }
        [Required]
        [Compare(nameof(Password))]
        public string CfmPassword { get; set; }
    }
}
