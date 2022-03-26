using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Data
{
    public class Dose
    {
        public int Id { get; set; }
        public int DayRange { get; set; }

        // Navigation Property
        public int VaccineId { get; set; }
        public Vaccine Vaccine { get; set; }
    }
}
