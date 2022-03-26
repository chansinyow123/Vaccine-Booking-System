using Microsoft.AspNetCore.Http;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Models.Customer
{
    public class EditCustomerModel
    {
        [Required]
        [MaxLength(30, ErrorMessage = "Must below 30 characters.")]
        public string Name { get; set; }
        [Required]
        [MaxLength(100, ErrorMessage = "Must below 100 characters.")]
        public string Address { get; set; }
        [Required]
        [RegularExpression("^[0-9]*$", ErrorMessage = "Only allow digit without '-'.")]
        [StringLength(12, MinimumLength = 12, ErrorMessage = "Must exactly 12 digit.")]
        public string IC { get; set; }
        [Required]
        [RegularExpression("^[0-9]*$", ErrorMessage = "Only allow digit without '-'.")]
        [StringLength(11, MinimumLength = 10, ErrorMessage = "Must between 10 to 11 digit.")]
        public string PhoneNumber { get; set; }
        public IFormFile File { get; set; } = null;
        public bool IsFileUpload { get; set; } = false;
    }
}
