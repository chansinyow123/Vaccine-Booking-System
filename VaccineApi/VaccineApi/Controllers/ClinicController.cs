using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.ModelBinding;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using VaccineApi.Data;
using VaccineApi.Models.Clinic;
using VaccineApi.Models.Vaccine;
using VaccineApi.Services;
using VaccineApi.Static;

namespace VaccineApi.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class ClinicController : ControllerBase
    {
        private readonly UserManager<ApplicationUser> _userManager;
        private readonly IFileService _fileService;
        private readonly ApplicationDbContext _context;

        public ClinicController(UserManager<ApplicationUser> userManager, IFileService fileService, ApplicationDbContext context)
        {
            _userManager = userManager;
            _fileService = fileService;
            _context = context;
        }

        [HttpGet("{id}/vaccines")]
        public async Task<IActionResult> GetClinicVaccines([FromRoute] string id)
        {
            // get the clinic account --------------------------------------------------------------
            var clinic = await _context.Users
                .AsNoTracking()
                .AsSplitQuery()
                .Include(c => c.Vaccines.Where(v => v.DeleteDateTime == null))
                    .ThenInclude(v => v.Doses.OrderBy(d => d.Id))
                .SingleOrDefaultAsync(c => c.Id == id);
            if (clinic == null) return NotFound();

            // check if the id is really a customer or not -----------------------------------------
            var role = await _userManager.GetRolesAsync(clinic);
            if (role[0] != UserRoles.Clinic) return NotFound();

            // prepare model -----------------------------------------------------------------------
            var model = new ClinicVaccineListModel
            {
                ClinicId = clinic.Id,
                Address = clinic.Address,
                Email = clinic.Email,
                File = clinic.File,
                ContentType = clinic.ContentType,
                Vaccines = clinic.Vaccines.Select(v => new VaccineModel
                {
                    Id = v.Id,
                    Name = v.Name,
                    Description = v.Description,
                    PricePerDose = v.PricePerDose,
                    DayRange = v.Doses.Select(d => d.DayRange).ToList()
                }).ToList()
            };

            // return model
            return Ok(model);
        }

        [HttpPost("")]
        [Authorize(Roles = UserRoles.Admin)]
        public async Task<IActionResult> RegisterClinic([FromForm] RegisterClinicModel model)
        {
            // Check if user already exist in database ----------------------------------------------
            var userExists = await _userManager.FindByNameAsync(model.Username);
            if (userExists != null)
            {
                ModelState.AddModelError(nameof(model.Username), "User Already Exist");
                return ValidationProblem(ModelState);
            }

            // Create User Instance -----------------------------------------------------------------
            ApplicationUser user = new ApplicationUser()
            {
                Email = model.Username,
                UserName = model.Username,
                Address = model.Address
                //File = photoContent,
                //ContentType = registerModel.Image.ContentType
            };

            // If Image is not null, Get the Photo MemoryStream and put into database ----------------------------
            if (model.File != null)
            {
                // specify the permittedExtension -------------------------------------------------------
                string[] permittedExtensions = { ".jpg", ".jpeg", ".png" };
                long fileSizeLimit = 20971520; // 20mb

                var fileByte = await _fileService.ProcessFormFile(model.File, ModelState, permittedExtensions, fileSizeLimit);

                // if error in file, return and display them --------------------------------------------
                if (!ModelState.IsValid)
                    return ValidationProblem(ModelState);

                // save file byte data to database ------------------------------------------------------
                user.File = fileByte;
                user.ContentType = model.File.ContentType;
            }

            // Create User in database --------------------------------------------------------------
            var result = await _userManager.CreateAsync(user, model.Password);
            if (!result.Succeeded)
            {
                // loop through error and put into modelstate
                foreach (var e in result.Errors)
                    ModelState.AddModelError(nameof(model.Password), e.Description);

                return ValidationProblem(ModelState);
            }

            // add to ST role -----------------------------------------------------------------------
            await _userManager.AddToRoleAsync(user, UserRoles.Clinic);

            // Verify email confirmation automatically ----------------------- ----------------------
            var token = await _userManager.GenerateEmailConfirmationTokenAsync(user);
            await _userManager.ConfirmEmailAsync(user, token);

            return Ok();
        }

        [HttpPut("")]
        [Authorize(Roles = UserRoles.Admin)]
        public async Task<IActionResult> EditClinic([FromForm] EditClinicModel model)
        {
            // Check is user exist or not, the ModelState is passed by reference ------------------
            // if not valid, return error message
            var user = await CheckIsUserExist(model.UserId);
            if (user == null) return NotFound();

            // update user details ---------------------------------------------------------------
            user.Address = model.Address;

            // if want to apply file upload
            if (model.IsFileUpload)
            {
                // If Image is not null, Get the Photo ByteArray and put into database ----------------------------
                if (model.File != null)
                {
                    // specify the permittedExtension -------------------------------------------------------
                    string[] permittedExtensions = { ".jpg", ".jpeg", ".png" };
                    long fileSizeLimit = 20971520; // 20mb

                    var fileByte = await _fileService.ProcessFormFile(model.File, ModelState, permittedExtensions, fileSizeLimit);

                    // if error in file, return and display them --------------------------------------------
                    if (!ModelState.IsValid)
                        return ValidationProblem(ModelState);

                    // save file byte data to database ------------------------------------------------------
                    user.File = fileByte;
                    user.ContentType = model.File.ContentType;
                }
                else
                {
                    // else set the file to null
                    user.File = null;
                    user.ContentType = null;
                }
            }

            // check if there is any error -------------------------------------------------------
            var result = await _userManager.UpdateAsync(user);
            if (!result.Succeeded) return StatusCode(StatusCodes.Status500InternalServerError);

            return Ok();
        }

        [HttpDelete("{id}")]
        [Authorize(Roles = UserRoles.Admin)]
        public async Task<IActionResult> DeleteClinic([FromRoute] string id)
        {
            // Check is user exist or not, the ModelState is passed by reference ------------------
            // if not valid, return error message
            var user = await CheckIsUserExist(id);
            if (user == null) return NotFound();

            // set the delete datetime to current datetime ---------------------------------------
            user.DeleteDateTime = DateTime.Now;
            var result = await _userManager.UpdateAsync(user);
            if (!result.Succeeded) return StatusCode(StatusCodes.Status500InternalServerError);

            return Ok();
        }

        [HttpPost("undo-delete/{id}")]
        [Authorize(Roles = UserRoles.Admin)]
        public async Task<IActionResult> UndoDeleteClinic([FromRoute] string id)
        {
            // Check is user exist or not, the ModelState is passed by reference ------------------
            // if not valid, return error message
            var user = await CheckIsUserExist(id);
            if (user == null) return NotFound();

            // set the delete datetime to current datetime ---------------------------------------
            user.DeleteDateTime = null;
            var result = await _userManager.UpdateAsync(user);
            if (!result.Succeeded) return StatusCode(StatusCodes.Status500InternalServerError);

            return Ok();
        }

        private async Task<ApplicationUser> CheckIsUserExist(string userId)
        {
            // get the user from database ---------------------------------------------------------
            var user = await _userManager.FindByIdAsync(userId);
            if (user == null) return null;

            // check if the role is really clinic ------------------------------------------------
            var role = await _userManager.GetRolesAsync(user);
            if (role[0] != UserRoles.Clinic) return null;

            return user;
        }


    }
}
