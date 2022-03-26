using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc.ModelBinding;
using System.Threading.Tasks;

namespace VaccineApi.Services
{
    public interface IFileService
    {
        Task<byte[]> ProcessFormFile(IFormFile formFile, ModelStateDictionary modelState, string[] permittedExtensions, long fileSizeLimit);
    }
}