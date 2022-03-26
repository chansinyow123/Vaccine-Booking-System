using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Data
{
    public class Appointment
    {
        public int Id { get; set; }
        public DateTime AppointmentDateTime { get; set; }
        public DateTime? AttendDateTime { get; set; }

        // Navigation Property
        public int BookingId { get; set; }
        public Booking Booking { get; set; }
    }
}
