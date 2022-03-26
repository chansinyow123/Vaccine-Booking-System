using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Data
{
    public class Booking
    {
        public int Id { get; set; }
        public DateTime CreateDateTime { get; set; }
        [Column(TypeName = "decimal(18,2)")]
        public decimal PricePerDose { get; set; }
        public string Description { get; set; }
        public DateTime? CancelDateTime { get; set; }

        // Navigation property
        public string CustomerId { get; set; }
        public ApplicationUser Customer { get; set; }
        public int VaccineId { get; set; }
        public Vaccine Vaccine { get; set; }
        public List<Appointment> Appointments { get; set; }
    }
}
