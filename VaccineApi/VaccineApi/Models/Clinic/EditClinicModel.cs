using Microsoft.AspNetCore.Http;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;
using VaccineApi.Validation;

namespace VaccineApi.Models.Clinic
{
    public class EditClinicModel
    {
        public string UserId { get; set; }
        [Required]
        [MaxLength(100, ErrorMessage = "Must below 100 characters.")]
        public string Address { get; set; }
        public IFormFile File { get; set; } = null;
        public bool IsFileUpload { get; set; } = false;
    }
}
