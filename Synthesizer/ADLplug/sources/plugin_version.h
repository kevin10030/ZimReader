//          Copyright Jean Pierre Cimalando 2018.
// Distributed under the Boost Software License, Version 1.0.
//    (See accompanying file LICENSE or copy at
//          http://www.boost.org/LICENSE_1_0.txt)

#pragma once
#include "AppConfig.h"

#define ADLplug_Version JucePlugin_VersionString
#define ADLplug_VersionFinal 1

#if !ADLplug_VersionFinal
#   define ADLplug_VersionExtra "Beta 5"
#   define ADLplug_SemVer JucePlugin_VersionString "-beta.5"
#else
#   define ADLplug_VersionExtra ""
#   define ADLplug_SemVer JucePlugin_VersionString
#endif
