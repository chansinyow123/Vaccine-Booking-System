using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Static
{
    public static class UserRoles
    {
        public const string Customer          = "Customer";
        public const string Clinic            = "Clinic";
        public const string Admin             = "Admin";
        public const string CustomerAndClinic = Customer + "," + Clinic;
    }
}
