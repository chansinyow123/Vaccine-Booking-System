using Microsoft.AspNetCore.Identity;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Data
{
    public class ApplicationUser : IdentityUser
    {
        public string Name { get; set; }
        public string IC { get; set; } // for customer
        public byte[] File { get; set; }
        public string ContentType { get; set; }
        public string Address { get; set; }
        public DateTime? DeleteDateTime { get; set; }

        //  Navigation Property
        public List<Vaccine> Vaccines { get; set; } // for clinic
        public List<Booking> Bookings { get; set; } // for customer
    }
}
