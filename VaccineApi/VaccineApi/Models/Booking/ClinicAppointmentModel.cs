using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Models.Booking
{
    public class ClinicAppointmentModel
    {
        public DateTime AppointmentTime { get; set; }
        public List<CustomerListModel> Customers { get; set; }
    }
}
