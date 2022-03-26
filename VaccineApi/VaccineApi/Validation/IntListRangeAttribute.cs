using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;

namespace VaccineApi.Validation
{
    public class IntListRangeAttribute : ValidationAttribute
    {
        private readonly int _min;
        private readonly int _max;

        public IntListRangeAttribute(int min, int max) : base($"range must between {min} to {max}.")
        {
            _min = min;
            _max = max;
        }

        public override bool IsValid(object value)
        {
            // Cast to list of int
            var list = value as List<int>;

            // if list is not null, and its max value is larger than _max, or its min value is smaller than _min
            // then return false
            return (list == null || list.Count == 0 || (list.Max() <= _max && list.Min() >= _min));
        }
    }
}
