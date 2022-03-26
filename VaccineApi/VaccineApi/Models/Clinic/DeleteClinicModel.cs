using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Models.Clinic
{
    public class DeleteClinicModel
    {
        [Required]
        public string UserId { get; set; }
    }
}
