//          Copyright Jean Pierre Cimalando 2018.
// Distributed under the Boost Software License, Version 1.0.
//    (See accompanying file LICENSE or copy at
//          http://www.boost.org/LICENSE_1_0.txt)

#pragma once
#include "JuceHeader.h"

struct Emulator_Defaults;
Emulator_Defaults &get_emulator_defaults();

struct Emulator_Defaults {
    StringArray choices;
    std::unique_ptr<Image[]> images;
    unsigned default_index = 0;
};

struct Chip_Settings {
    unsigned emulator = 0;
    unsigned chip_count = 2;
    unsigned fourop_count = 0;

    PropertySet to_properties() const;
    static Chip_Settings from_properties(const PropertySet &set);
};

inline bool operator==(const Chip_Settings &a, const Chip_Settings &b)
{
    return a.emulator == b.emulator && a.chip_count == b.chip_count && a.fourop_count == b.fourop_count;
}

inline bool operator!=(const Chip_Settings &a, const Chip_Settings &b)
{
    return !operator==(a, b);
}
