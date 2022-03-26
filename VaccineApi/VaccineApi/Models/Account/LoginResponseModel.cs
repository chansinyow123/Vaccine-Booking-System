using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Models.Account
{
    public class LoginResponseModel
    {
        public string Token { get; set; }
        public string UserRole { get; set; }
        public string Name { get; set; }
        public string Email { get; set; }
        public string Address { get; set; }
        public byte[] File { get; set; }
        public string ContentType { get; set; }
    }
}
