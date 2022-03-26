using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;
using VaccineApi.Validation;

namespace VaccineApi.Models.Booking
{
    public class CreateBookingModel
    {
        public int VaccineId { get; set; }
        [MaxLength(150, ErrorMessage = "Must below 150 characters.")]
        public string Description { get; set; } = "";
        [DayRange(min: 0, max: 30, ErrorMessage = "The appointment date must within 30 days from now on.")]
        [OperationHour]
        public DateTime AppointmentDateTime { get; set; }
    }
}
