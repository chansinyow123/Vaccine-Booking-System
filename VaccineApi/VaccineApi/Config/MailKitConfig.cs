using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Config
{
    public class MailKitConfig
    {
        public string Server { get; set; }
        public int Port { get; set; }
        public string SenderName { get; set; }
        public string SenderEmail { get; set; }
        public string Account { get; set; }
        public string Password { get; set; }
        public bool RequireSSL { get; set; }
    }
}
