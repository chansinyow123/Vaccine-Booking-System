using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Models.Booking
{
    public class CustomerListModel
    {
        public int BookingId { get; set; }
        public byte[] File { get; set; }
        public string ContentType { get; set; }
        public string Name { get; set; }
        public string Email { get; set; }
    }
}
