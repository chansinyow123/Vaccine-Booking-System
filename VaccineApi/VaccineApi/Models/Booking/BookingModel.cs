using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using VaccineApi.Models.Vaccine;

namespace VaccineApi.Models.Booking
{
    public class BookingModel
    {
        public int BookingId { get; set; }
        public byte[] File { get; set; }
        public string ContentType { get; set; }
        public string Name { get; set; }
        public string Email { get; set; }
        public string IC { get; set; }
        public string PhoneNumber { get; set; }
        public string ClinicAddress { get; set; }
        public string ClinicEmail { get; set; }
        public string BookingDescription { get; set; }
        public string VaccineName { get; set; }
        public string VaccineDescription { get; set; }
        public decimal VaccinePricePerDose { get; set; }
        public List<int> DayRange { get; set; }
        public List<AppointmentModel> Appointments { get; set; }
        // false = Cancelled
        // true  = Completed
        // null  = Progressing
        public bool? Completed { get; set; }

    }
}
