using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Models.Vaccine
{
    public class DeleteVaccineModel
    {
        [Required]
        public int Id { get; set; }
    }
}
