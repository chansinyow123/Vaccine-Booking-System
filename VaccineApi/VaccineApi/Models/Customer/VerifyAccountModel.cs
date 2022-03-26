using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Models.Customer
{
    public class VerifyAccountModel
    {
        [Required]
        public string UserId { get; set; }
        [Required]
        public string Token { get; set; }
    }
}
