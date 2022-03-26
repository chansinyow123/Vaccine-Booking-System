using AutoMapper;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using VaccineApi.Data;
using VaccineApi.Models.Account;
using VaccineApi.Models.Customer;
using VaccineApi.Models.Vaccine;

namespace VaccineApi.Helpers
{
    public class ApplicationMapper : Profile
    {
        public ApplicationMapper()
        {
            // https://stackoverflow.com/questions/34271334/automapper-how-to-map-nested-object
            // Vaccine Controller --------------------------------------------------------------
            CreateMap<Vaccine, VaccineModel>()
                .ForMember(pts => pts.DayRange, opt => opt.MapFrom(ps => ps.Doses.Select(x => x.DayRange)))
                .ReverseMap();
            CreateMap<Vaccine, EditVaccineModel>().ReverseMap();

            // Account Controller -------------------------------------------------------------
            CreateMap<ApplicationUser, UserModel>().ReverseMap();

            // Booking Controller -------------------------------------------------------------
        }
    }
}
