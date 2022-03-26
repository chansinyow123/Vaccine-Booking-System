using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Data
{
    public class Vaccine
    {
        public int Id { get; set; }
        public string Name { get; set; }
        public string Description { get; set; }
        [Column(TypeName = "decimal(18,2)")]
        public decimal PricePerDose { get; set; }
        public DateTime? DeleteDateTime { get; set; }

        // Navigation Property
        public string ClinicId { get; set; }
        public ApplicationUser Clinic { get; set; }
        public List<Booking> Bookings { get; set; }
        public List<Dose> Doses { get; set; }
    }
}
