using Microsoft.AspNetCore.Http;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Models.Account
{
    public class UpdateImageModel
    {
        public IFormFile File { get; set; }
    }
}
