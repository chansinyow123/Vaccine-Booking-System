using AutoMapper;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using VaccineApi.Data;
using VaccineApi.Models.Vaccine;
using VaccineApi.Static;
using Microsoft.EntityFrameworkCore;

namespace VaccineApi.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class VaccineController : ControllerBase
    {
        private readonly ApplicationDbContext _context;
        private readonly UserManager<ApplicationUser> _userManager;
        private readonly IMapper _mapper;

        public VaccineController(ApplicationDbContext context, UserManager<ApplicationUser> userManager, IMapper mapper)
        {
            _context = context;
            _userManager = userManager;
            _mapper = mapper;
        }

        [HttpGet("clinic")]
        [Authorize(Roles = UserRoles.Clinic)]
        public async Task<IActionResult> GetAllVaccinesByClinic()
        {
            var clinic = await _userManager.FindByNameAsync(User.Identity.Name);
            if (clinic == null) return NotFound();

            // get the vaccine that is not deleted, if null then return NotFound ------------------------------------------------------
            var data = await _context.Vaccines
                .AsNoTracking()
                .Include(v => v.Doses.OrderBy(d => d.Id))
                .Where(v => v.DeleteDateTime == null && v.ClinicId == clinic.Id)
                .OrderBy(v => v.Name)
                .ToListAsync();

            if (data == null) return NotFound();

            var model = _mapper.Map<List<VaccineModel>>(data);

            return Ok(model);
        }

        [HttpGet("")]
        public async Task<IActionResult> GetAllVaccines()
        {
            // get the vaccine that is not deleted, if null then return NotFound ------------------------------------------------------
            var data = await _context.Vaccines
                .AsNoTracking()
                .Include(v => v.Doses.OrderBy(d => d.Id))
                .Where(v => v.DeleteDateTime == null)
                .ToListAsync();

            if (data == null) return NotFound();

            var model = _mapper.Map<List<VaccineModel>>(data);

            return Ok(model);
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> GetVaccineById([FromRoute] int id)
        {
            // get the data by finding id, if null then return NotFound --------------------------------------------------
            var data = await _context.Vaccines
                .AsNoTracking()
                .Include(v => v.Doses.OrderBy(d => d.Id))
                .SingleOrDefaultAsync(v => v.Id == id);

            if (data == null) return NotFound();

            var model = _mapper.Map<VaccineModel>(data);

            return Ok(model);
        }

        [HttpPost("")]
        [Authorize(Roles = UserRoles.Clinic)]
        public async Task<IActionResult> CreateVaccine([FromBody] CreateVaccineModel model)
        {
            // if clinic has been deleted, return 403 forbid status code ---------------------------------------------
            var clinic = await IsClinicDeleted();
            if (clinic == null) return StatusCode(StatusCodes.Status403Forbidden);

            // prepare vaccine data -----------------------------------------------------------------------------------
            var vaccine = new Vaccine
            {
                Name = model.Name,
                Description = model.Description,
                PricePerDose = model.PricePerDose,
                Doses = model.DayRange
                            .Select(x => new Dose { DayRange = x })
                            .ToList(),
                Clinic = clinic
            };

            // save changes -------------------------------------------------------------------------------------------
            _context.Vaccines.Add(vaccine);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetVaccineById), new { id = vaccine.Id, controller = "vaccine" }, vaccine.Id);
        }

        [HttpPut("")]
        [Authorize(Roles = UserRoles.Clinic)]
        public async Task<IActionResult> EditVaccine([FromBody] EditVaccineModel model)
        {
            // if clinic has been deleted, return 403 forbid status code ---------------------------------------------
            var clinic = await IsClinicDeleted();
            if (clinic == null) return StatusCode(StatusCodes.Status403Forbidden);

            // find the data in vaccine table by VaccineId and ClinicId  ----------------------------------------------
            var data = await _context.Vaccines.SingleOrDefaultAsync(v => v.Id == model.Id && v.ClinicId == clinic.Id);
            if (data == null) return NotFound();

            // update the database ------------------------------------------------------------------------------------
            _mapper.Map(model, data);
            await _context.SaveChangesAsync();

            return Ok();
        }

        [HttpDelete("{id}")]
        [Authorize(Roles = UserRoles.Clinic)]
        public async Task<IActionResult> DeleteVaccine([FromRoute] int id)
        {
            // if clinic has been deleted, return 403 forbid status code ---------------------------------------------
            var clinic = await IsClinicDeleted();
            if (clinic == null) return StatusCode(StatusCodes.Status403Forbidden);

            // find the data in vaccine table by VaccineId and ClinicId ----------------------------------------------
            var data = await _context.Vaccines.SingleOrDefaultAsync(v => v.Id == id && v.ClinicId == clinic.Id);
            if (data == null) return NotFound();

            // update the database -----------------------------------------------------------------------------------
            data.DeleteDateTime = DateTime.Now;
            await _context.SaveChangesAsync();

            return Ok();
        }

        [HttpPost("undo-delete/{id}")]
        [Authorize(Roles = UserRoles.Clinic)]
        public async Task<IActionResult> UndoDeleteVaccine([FromRoute] int id)
        {
            // if clinic has been deleted, return 403 forbid status code ---------------------------------------------
            var clinic = await IsClinicDeleted();
            if (clinic == null) return StatusCode(StatusCodes.Status403Forbidden);

            // find the data in vaccine table by VaccineId and ClinicId ----------------------------------------------
            var data = await _context.Vaccines.SingleOrDefaultAsync(v => v.Id == id && v.ClinicId == clinic.Id);
            if (data == null) return NotFound();

            // update the database -----------------------------------------------------------------------------------
            data.DeleteDateTime = null;
            await _context.SaveChangesAsync();

            return Ok();
        }

        private async Task<ApplicationUser> IsClinicDeleted()
        {
            var user = await _userManager.FindByNameAsync(User.Identity.Name);
            // if user is not deleted, return user, else return null
            if (user.DeleteDateTime == null) return user;
            else return null;
        }
    }
}
