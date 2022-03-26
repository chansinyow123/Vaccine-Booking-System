using Microsoft.AspNetCore.Http;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Models.Clinic
{
    public class RegisterClinicModel
    {
        [EmailAddress]
        [Required]
        public string Username { get; set; }
        [Required]
        public string Password { get; set; }
        [Required]
        [MaxLength(100, ErrorMessage = "Must below 100 characters.")]
        public string Address { get; set; }
        public IFormFile File { get; set; }
    }
}
