using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Models.Booking
{
    public class CustomerBookingModel
    {
        public int Id { get; set; }
        public string VaccineName { get; set; }
        public string ClinicAddress { get; set; }
        // false = Cancelled
        // true  = Completed
        // null  = Progressing
        public bool? Completed { get; set; }
        public DateTime ActionDateTime { get; set; }
    }
}
