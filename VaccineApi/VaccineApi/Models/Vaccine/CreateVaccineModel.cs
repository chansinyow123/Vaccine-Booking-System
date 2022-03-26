using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;
using VaccineApi.Validation;

namespace VaccineApi.Models.Vaccine
{
    public class CreateVaccineModel
    {
        [Required]
        [MaxLength(30, ErrorMessage = "Must below 30 characters.")]
        public string Name { get; set; }
        [Required]
        [MaxLength(500, ErrorMessage = "Must below 500 characters.")]
        public string Description { get; set; }
        [Range(0, 99999.99, ErrorMessage = "Must between 0.00 to 99999.99.")]
        public decimal PricePerDose { get; set; }
        [MaxList(max: 3)]
        [IntListRange(max: 1825, min: 1, ErrorMessage = "The day range must between 1 to 1825 day.")]
        public List<int> DayRange { get; set; }
    }
}
