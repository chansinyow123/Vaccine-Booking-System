using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc.ModelBinding;
using Microsoft.Extensions.Configuration;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;

namespace VaccineApi.Services
{
    public class FileService : IFileService
    {
        // Here is the references for these implementation: https://github.com/dotnet/AspNetCore.Docs/tree/main/aspnetcore/mvc/models/file-uploads/samples/3.x/SampleApp
        // If you require a check on specific characters in the IsValidFileExtensionAndSignature
        // method, supply the characters in the _allowedChars field.
        private readonly byte[] _allowedChars = { };
        // For more file signatures, see the File Signatures Database (https://www.filesignatures.net/)
        // and the official specifications for the file types you wish to add.
        private readonly Dictionary<string, List<byte[]>> _fileSignature = new Dictionary<string, List<byte[]>>
        {
            { ".gif", new List<byte[]> { new byte[] { 0x47, 0x49, 0x46, 0x38 } } },
            { ".png", new List<byte[]> { new byte[] { 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A } } },
            { ".jpeg", new List<byte[]>
                {
                    new byte[] { 0xFF, 0xD8, 0xFF, 0xE0 },
                    new byte[] { 0xFF, 0xD8, 0xFF, 0xE2 },
                    new byte[] { 0xFF, 0xD8, 0xFF, 0xE3 },
                }
            },
            { ".jpg", new List<byte[]>
                {
                    new byte[] { 0xFF, 0xD8, 0xFF, 0xE0 },
                    new byte[] { 0xFF, 0xD8, 0xFF, 0xE1 },
                    new byte[] { 0xFF, 0xD8, 0xFF, 0xE8 },
                }
            },
            { ".zip", new List<byte[]>
                {
                    new byte[] { 0x50, 0x4B, 0x03, 0x04 },
                    new byte[] { 0x50, 0x4B, 0x4C, 0x49, 0x54, 0x45 },
                    new byte[] { 0x50, 0x4B, 0x53, 0x70, 0x58 },
                    new byte[] { 0x50, 0x4B, 0x05, 0x06 },
                    new byte[] { 0x50, 0x4B, 0x07, 0x08 },
                    new byte[] { 0x57, 0x69, 0x6E, 0x5A, 0x69, 0x70 },
                    new byte[] { 0x50, 0x4B, 0x03, 0x04, 0x14, 0x00, 0x01, 0x00 },
                }
            },
            { ".doc", new List<byte[]>
                {
                    new byte[] { 0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1 },
                    new byte[] { 0x0D, 0x44, 0x4F, 0x43 },
                    new byte[] { 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1, 0x00 },
                    new byte[] { 0xDB, 0xA5, 0x2D, 0x00 },
                    new byte[] { 0xEC, 0xA5, 0xC1, 0x00 },
                }
            },
            { ".docx", new List<byte[]>
                {
                    new byte[] { 0x50, 0x4B, 0x03, 0x04 },
                    new byte[] { 0x50, 0x4B, 0x03, 0x04, 0x14, 0x00, 0x06, 0x00 },
                }
            },
        };

        // **WARNING!**
        // In the following file processing methods, the file's content isn't scanned.
        // In most production scenarios, an anti-virus/anti-malware scanner API is
        // used on the file before making the file available to users or other
        // systems. For more information, see the topic that accompanies this sample
        // app.

        public async Task<byte[]> ProcessFormFile(IFormFile formFile, ModelStateDictionary modelState, string[] permittedExtensions, long fileSizeLimit)
        {

            // Check the file length. This check doesn't catch files that only have 
            // a BOM as their content.
            if (formFile.Length == 0)
            {
                modelState.AddModelError("File", "File is empty.");
                return Array.Empty<byte>();
            }

            // Check is fileSizeLimit is exceeded
            if (formFile.Length > fileSizeLimit)
            {
                var megabyteSizeLimit = fileSizeLimit / 1048576;
                modelState.AddModelError("File", $"File exceeds {megabyteSizeLimit:N1} MB.");

                return Array.Empty<byte>();
            }

            try
            {
                using (var memoryStream = new MemoryStream())
                {
                    await formFile.CopyToAsync(memoryStream);

                    // Check the content length in case the file's only
                    // content was a BOM and the content is actually
                    // empty after removing the BOM.
                    if (memoryStream.Length == 0)
                    {
                        modelState.AddModelError("File", "File is empty.");
                        return Array.Empty<byte>();
                    }

                    // Check if file extension and file signature is valid
                    if (!IsValidFileExtensionAndSignature(formFile.FileName, memoryStream, permittedExtensions))
                    {
                        modelState.AddModelError("File", $"File extension must be {string.Join(", ", permittedExtensions)}.");
                        return Array.Empty<byte>();
                    }
                        
                    return memoryStream.ToArray();
                }
            }
            catch (Exception ex)
            {
                // Log the exception
                modelState.AddModelError(formFile.Name, $"File upload failed. Error: {ex.Message}");
            }

            return Array.Empty<byte>();
        }

        private bool IsValidFileExtensionAndSignature(string fileName, Stream data, string[] permittedExtensions)
        {
            // check if filename or stream data exist or not
            if (string.IsNullOrEmpty(fileName) || data == null || data.Length == 0)
            {
                return false;
            }

            // Check the extension of the file
            var ext = Path.GetExtension(fileName).ToLowerInvariant();
            if (string.IsNullOrEmpty(ext) || !permittedExtensions.Contains(ext))
            {
                return false;
            }

            // reset the stream position for later reading
            data.Position = 0;

            // read the binary from stream to validate allowedChars
            using (var reader = new BinaryReader(data))
            {
                if (ext.Equals(".txt") || ext.Equals(".csv") || ext.Equals(".prn"))
                {
                    if (_allowedChars.Length == 0)
                    {
                        // Limits characters to ASCII encoding.
                        for (var i = 0; i < data.Length; i++)
                        {
                            if (reader.ReadByte() > sbyte.MaxValue)
                            {
                                return false;
                            }
                        }
                    }
                    else
                    {
                        // Limits characters to ASCII encoding and
                        // values of the _allowedChars array.
                        for (var i = 0; i < data.Length; i++)
                        {
                            var b = reader.ReadByte();

                            if (b > sbyte.MaxValue || !_allowedChars.Contains(b))
                            {
                                return false;
                            }
                        }
                    }

                    return true;
                }

                // Uncomment the following code block if you must permit
                // files whose signature isn't provided in the _fileSignature
                // dictionary. We recommend that you add file signatures
                // for files (when possible) for all file types you intend
                // to allow on the system and perform the file signature
                // check.
                /*
                if (!_fileSignature.ContainsKey(ext))
                {
                    return true;
                }
                */

                // File signature check
                // --------------------
                // With the file signatures provided in the _fileSignature
                // dictionary, the following code tests the input content's
                // file signature.
                var signatures = _fileSignature[ext];
                var headerBytes = reader.ReadBytes(signatures.Max(m => m.Length));

                return signatures.Any(signature =>
                    headerBytes.Take(signature.Length).SequenceEqual(signature));
            }
        }
    }
}
