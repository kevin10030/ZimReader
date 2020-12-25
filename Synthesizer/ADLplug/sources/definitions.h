//          Copyright Jean Pierre Cimalando 2018.
// Distributed under the Boost Software License, Version 1.0.
//    (See accompanying file LICENSE or copy at
//          http://www.boost.org/LICENSE_1_0.txt)

#pragma once

// bank slors to reserve in the synthesizer
static constexpr unsigned bank_reserve_size = 64;

// maximum program notifications in a cycle
static constexpr unsigned max_program_notifications = 32;

// maximum program measurement requests in a cycle
static constexpr unsigned max_program_measurements = 32;

// maximum interval between midi processing cycles
static constexpr unsigned midi_interval_max = 256;
