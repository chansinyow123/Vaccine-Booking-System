using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Models.Booking
{
    public class AppointmentModel
    {
        public DateTime AppointmentDateTime { get; set; }
        public DateTime? AttendDateTime { get; set; }
    }
}
