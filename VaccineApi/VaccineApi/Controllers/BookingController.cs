using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using VaccineApi.Data;
using VaccineApi.Models.Booking;
using VaccineApi.Static;

namespace VaccineApi.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class BookingController : ControllerBase
    {
        private readonly ApplicationDbContext _context;
        private readonly UserManager<ApplicationUser> _userManager;

        public BookingController(ApplicationDbContext context, UserManager<ApplicationUser> userManager)
        {
            _context = context;
            _userManager = userManager;
        }

        [HttpPost("")]
        [Authorize(Roles = UserRoles.Customer)]
        public async Task<IActionResult> CreateBooking([FromBody] CreateBookingModel model)
        {
            // check if customer username exist or not ---------------------------------------------------------------
            var customer = await _userManager.FindByNameAsync(User.Identity.Name);
            if (customer == null) return NotFound();

            // check if vaccine id exist or not ----------------------------------------------------------------------
            var vaccine = _context.Vaccines.Find(model.VaccineId);
            if (vaccine == null) return NotFound();

            // add booking into database -----------------------------------------------------------------------------
            var booking = new Booking
            {
                Customer        = customer,
                Vaccine         = vaccine,
                CreateDateTime  = DateTime.Now,
                PricePerDose    = vaccine.PricePerDose,
                Description     = model.Description,
                Appointments    = new List<Appointment> { 
                                    new Appointment { AppointmentDateTime = model.AppointmentDateTime } 
                                },
            };

            _context.Bookings.Add(booking);
            await _context.SaveChangesAsync();

            return Ok();
        }

        [HttpGet("customer")]
        [Authorize(Roles = UserRoles.Customer)]
        public async Task<IActionResult> GetCustomerAppointmentList()
        {
            // check if customer username exist or not ---------------------------------------------------------------
            var customer = await _userManager.FindByNameAsync(User.Identity.Name);
            if (customer == null) return NotFound();

            var model = await _context.Bookings
                .AsNoTracking()
                .Include(b => b.Vaccine)
                    .ThenInclude(v => v.Clinic)
                .Include(b => b.Vaccine)
                    .ThenInclude(v => v.Doses)
                .Include(b => b.Appointments)
                .AsSplitQuery()
                .Where(b => b.CustomerId == customer.Id)
                .Select(b => new CustomerBookingModel
                {
                    Id                 = b.Id,
                    VaccineName        = b.Vaccine.Name,
                    ClinicAddress      = b.Vaccine.Clinic.Address,
                    // false = Cancelled
                    // true  = Completed
                    // null  = Progressing
                    Completed = (b.CancelDateTime != null) 
                                            ? false 
                                            : (b.Appointments.Where(a => a.AttendDateTime == null).Any()) 
                                                ? null
                                                : true,
                    // 1) if CancelDateTime is not null    = Cancelled
                    // 2) if AttendDateTime dont have null = Completed
                    // 3) if AttendDateTime have null      = Progressing
                    ActionDateTime  = b.CancelDateTime
                                        ?? b.Appointments.OrderByDescending(a => a.AttendDateTime.HasValue).ThenBy(a => a.AttendDateTime).LastOrDefault().AttendDateTime 
                                        ?? b.Appointments.OrderByDescending(a => a.AttendDateTime.HasValue).ThenBy(a => a.AttendDateTime).LastOrDefault().AppointmentDateTime

                })
                .OrderBy(b => b.Completed)
                    .ThenByDescending(b => b.ActionDateTime)
                .ToListAsync();

            return Ok(model);
        }

        [HttpGet("{id}")]
        [Authorize]
        public async Task<IActionResult> GetBookingById([FromRoute] int id)
        {
            // get booking info -------------------------------------------------------------------------------
            var booking = await _context.Bookings
                .AsNoTracking()
                .Include(b => b.Customer)
                .Include(b => b.Vaccine)
                    .ThenInclude(v => v.Clinic)
                .Include(b => b.Vaccine)
                    .ThenInclude(v => v.Doses)
                .Include(b => b.Appointments)
                .AsSplitQuery()
                .SingleOrDefaultAsync(b => b.Id == id);

            if (booking == null)
                return NotFound();
            
            // if the user is customer, and the customerId is not own by the customer, response error -----------------
            if (User.IsInRole(UserRoles.Customer) && booking.Customer.UserName != User.Identity.Name)
                return StatusCode(StatusCodes.Status403Forbidden);

            // if the user is clinic, and the vaccineId is not own by the clinic, response error ----------------------
            if (User.IsInRole(UserRoles.Clinic) && booking.Vaccine.Clinic.UserName != User.Identity.Name)
                return StatusCode(StatusCodes.Status403Forbidden);

            var model = new BookingModel
            {
                BookingId           = booking.Id,
                File                = booking.Customer.File,
                ContentType         = booking.Customer.ContentType,
                Name                = booking.Customer.Name,
                Email               = booking.Customer.Email,
                IC                  = booking.Customer.IC,
                PhoneNumber         = booking.Customer.PhoneNumber,
                ClinicAddress       = booking.Vaccine.Clinic.Address,
                ClinicEmail         = booking.Vaccine.Clinic.Email,
                BookingDescription  = booking.Description,
                VaccineName         = booking.Vaccine.Name,
                VaccineDescription  = booking.Vaccine.Description,
                VaccinePricePerDose = booking.Vaccine.PricePerDose,
                DayRange            = booking.Vaccine.Doses.Select(d => d.DayRange).ToList(),
                Appointments        = booking.Appointments
                                        .Select(a => new AppointmentModel { 
                                            AppointmentDateTime = a.AppointmentDateTime,
                                            AttendDateTime = a.AttendDateTime
                                        })
                                        .ToList(),
                // false = Cancelled
                // true  = Completed
                // null  = Progressing
                Completed = (booking.CancelDateTime != null)
                                            ? false
                                            : (booking.Appointments.Where(a => a.AttendDateTime == null).Any())
                                                ? null
                                                : true,
            };

            return Ok(model);
        }

        [HttpPost("proceed")]
        [Authorize(Roles = UserRoles.Customer)]
        public async Task<IActionResult> ProceedBooking([FromBody] ProceedBookingModel model)
        {
            // get user data ------------------------------------------------------------------------------------------
            var customer = await _userManager.FindByNameAsync(User.Identity.Name);
            if (customer == null) return NotFound();

            // get booking data by BookingId and CustomerId -----------------------------------------------------------
            // and also check is qrcode match the clinic Id
            // also check is CancelDatetime is null
            var booking = await _context.Bookings
                .Include(b => b.Vaccine)
                    .ThenInclude(b => b.Doses)
                .Include(b => b.Appointments)
                .AsSplitQuery()
                .SingleOrDefaultAsync(b => 
                b.Id == model.Id && 
                b.CustomerId == customer.Id && 
                b.Vaccine.ClinicId == model.QRCode &&
                b.CancelDateTime == null);
            if (booking == null) return NotFound();

            // check if there are any AttendDateTime is null, if dont have, means the appointment is finished --------
            // then return NotFound()
            var lastAppointment = booking.Appointments.SingleOrDefault(b => b.AttendDateTime == null);
            if (lastAppointment == null) return NotFound();

            // update the attend datetime ----------------------------------------------------------------------------
            lastAppointment.AttendDateTime = DateTime.Now;

            // get the count of vaccine dose -------------------------------------------------------------------------
            var doseCount = booking.Vaccine.Doses.Count();
            // get the count of appointment minus 1
            // reason of minus 1 is because the first appointment is the first dose
            var appointmentCount = booking.Appointments.Count() - 1;

            // if there are additional dose, then add appointment ----------------------------------------------------
            if (doseCount > appointmentCount)
            {
                // get the dayRange of the vaccine
                var dayRange = booking.Vaccine.Doses.OrderBy(d => d.Id).ElementAt(appointmentCount).DayRange;
                // then add on todays datetime for next appointment
                var date = DateTime.Now.AddDays(dayRange);
                var time = lastAppointment.AppointmentDateTime;
                var next = new DateTime(date.Year, date.Month, date.Day, time.Hour, time.Minute, time.Second);
                // add to database
                booking.Appointments.Add(new Appointment { AppointmentDateTime = next });
            }

            // save changes
            await _context.SaveChangesAsync();

            return Ok();
        }

        [HttpDelete("{id}")]
        [Authorize(Roles = UserRoles.Customer)]
        public async Task<IActionResult> DeleteBooking([FromRoute] int id)
        {
            // get user data ------------------------------------------------------------------------------------------
            var customer = await _userManager.FindByNameAsync(User.Identity.Name);
            if (customer == null) return NotFound();

            // get booking data by BookingId and CustomerId -----------------------------------------------------------
            var booking = await _context.Bookings
                .Include(b => b.Appointments)
                .SingleOrDefaultAsync(b => b.Id == id && b.CustomerId == customer.Id);
            if (booking == null) return NotFound();

            // if all AttendDateTime is not null, means that the appointment is completed -----------------------------
            // therefore there is no point to delete booking, so return NotFound()
            if (booking.Appointments.SingleOrDefault(a => a.AttendDateTime == null) == null)
                return NotFound();

            // save changes -------------------------------------------------------------------------------------------
            booking.CancelDateTime = DateTime.Now;
            await _context.SaveChangesAsync();

            return Ok();
        }

        [HttpPost("undo-delete/{id}")]
        [Authorize(Roles = UserRoles.Customer)]
        public async Task<IActionResult> UndoDeleteBooking([FromRoute] int id)
        {
            // get user data ------------------------------------------------------------------------------------------
            var customer = await _userManager.FindByNameAsync(User.Identity.Name);
            if (customer == null) return NotFound();

            // get booking data by BookingId and CustomerId -----------------------------------------------------------
            var booking = await _context.Bookings.SingleOrDefaultAsync(b => b.Id == id && b.CustomerId == customer.Id);
            if (booking == null) return NotFound();

            // save changes -------------------------------------------------------------------------------------------
            booking.CancelDateTime = null;
            await _context.SaveChangesAsync();

            return Ok();
        }

        [HttpGet("appointments")]
        [Authorize(Roles = UserRoles.Clinic)]
        public async Task<IActionResult> ClinicAppointments([FromQuery] DateTime datetime)
        {
            // get user data ------------------------------------------------------------------------------------------
            var clinic = await _userManager.FindByNameAsync(User.Identity.Name);
            if (clinic == null) return NotFound();

            // find all the appointments that are relevant to this BookingId list
            var appointment = await _context.Appointments
                .AsNoTracking()
                .Include(a => a.Booking)
                    .ThenInclude(b => b.Customer)
                .Include(a => a.Booking)
                    .ThenInclude(a => a.Vaccine)
                .Where(a => a.Booking.Vaccine.ClinicId == clinic.Id)
                .Where(a => a.AppointmentDateTime.Date == datetime.Date)
                .ToListAsync();

            // then group by the appointmentdatetime
            // the reason why we need to split two linq syntax is because groupby does not support asynchronous method.
            var data = appointment
                .GroupBy(a => a.AppointmentDateTime)
                .Select(a => new ClinicAppointmentModel
                 {
                     AppointmentTime = a.Key,
                     Customers = a.Select(x => new CustomerListModel
                     {
                         BookingId = x.BookingId,
                         File = x.Booking.Customer.File,
                         ContentType = x.Booking.Customer.ContentType,
                         Name = x.Booking.Customer.Name,
                         Email = x.Booking.Customer.Email
                     })
                    .ToList()
                 }).ToList();

            return Ok(data);
        }
    }
}



















