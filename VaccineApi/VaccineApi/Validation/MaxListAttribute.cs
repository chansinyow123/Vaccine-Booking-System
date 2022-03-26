using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Validation
{
    public class MaxListAttribute : ValidationAttribute
    {
        private readonly int _max;

        public MaxListAttribute(int max) : base($"must below {max} count.")
        {
            _max = max;
        }

        public override bool IsValid(object value)
        {
            // Cast to list of int
            var list = value as List<int>;

            // if list is not null, and its max value is larger than _max
            // then return false
            return (list == null || list.Count <= _max);
        }
    }
}
