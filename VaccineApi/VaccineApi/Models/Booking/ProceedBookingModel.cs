using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Models.Booking
{
    public class ProceedBookingModel
    {
        [Required]
        public int Id { get; set; }
        [Required]
        public string QRCode { get; set; }
    }
}
