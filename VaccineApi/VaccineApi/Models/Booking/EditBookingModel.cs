using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using VaccineApi.Validation;

namespace VaccineApi.Models.Booking
{
    public class EditBookingModel
    {
        public int Id { get; set; }
        [OperationHour]
        public DateTime AppointmentDateTime { get; set; }
    }
}
