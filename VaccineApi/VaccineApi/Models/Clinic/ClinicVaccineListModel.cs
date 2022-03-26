using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using VaccineApi.Models.Vaccine;

namespace VaccineApi.Models.Clinic
{
    public class ClinicVaccineListModel
    {
        public string ClinicId { get; set; }
        public string Address { get; set; }
        public string Email { get; set; }
        public byte[] File { get; set; }
        public string ContentType { get; set; }
        public List<VaccineModel> Vaccines { get; set; }
    }
}
