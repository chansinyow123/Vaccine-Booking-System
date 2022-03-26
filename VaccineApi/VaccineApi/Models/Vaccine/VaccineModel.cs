using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using VaccineApi.Data;

namespace VaccineApi.Models.Vaccine
{
    public class VaccineModel
    {
        public int Id { get; set; }
        public string Name { get; set; }
        public string Description { get; set; }
        public decimal PricePerDose { get; set; }
        public List<int> DayRange { get; set; }
    }
}
