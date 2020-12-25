//          Copyright Jean Pierre Cimalando 2018.
// Distributed under the Boost Software License, Version 1.0.
//    (See accompanying file LICENSE or copy at
//          http://www.boost.org/LICENSE_1_0.txt)

#include "instrument.h"
#include "JuceHeader.h"

PropertySet Instrument::to_properties() const
{
    PropertySet set;

    // set.setValue("pseudo_eight_op", pseudo_eight_op());
    set.setValue("blank", blank());
    set.setValue("note_offset", (int)note_offset);
    set.setValue("feedback", (int)feedback());
    set.setValue("algorithm", (int)algorithm());
    set.setValue("ams", (int)ams());
    set.setValue("fms", (int)fms());
    set.setValue("midi_velocity_offset", (int)midi_velocity_offset);
    set.setValue("percussion_key_number", (int)percussion_key_number);

    for (unsigned opnum = 0; opnum < 4; ++opnum) {
        const String opfx = ((const char *[]){ "op1", "op3", "op2", "op4" })[opnum];
        set.setValue(opfx + "detune", (int)detune(opnum));
        set.setValue(opfx + "fmul", (int)fmul(opnum));
        set.setValue(opfx + "level", (int)level(opnum));
        set.setValue(opfx + "ratescale", (int)ratescale(opnum));
        set.setValue(opfx + "attack", (int)attack(opnum));
        set.setValue(opfx + "am", am(opnum));
        set.setValue(opfx + "decay1", (int)decay1(opnum));
        set.setValue(opfx + "decay2", (int)decay2(opnum));
        set.setValue(opfx + "sustain", (int)sustain(opnum));
        set.setValue(opfx + "release", (int)release(opnum));
        set.setValue(opfx + "ssgenable", ssgenable(opnum));
        set.setValue(opfx + "ssgwave", (int)ssgwave(opnum));
    }

    set.setValue("delay_off_ms", (int)delay_off_ms);
    set.setValue("delay_on_ms", (int)delay_on_ms);

    return set;
}

Instrument Instrument::from_properties(const juce::PropertySet &set)
{
    Instrument ins;

    // ins.pseudo_eight_op(set.getBoolValue("pseudo_eight_op"));
    ins.blank(set.getBoolValue("blank"));
    ins.note_offset = set.getIntValue("note_offset");
    ins.feedback(set.getIntValue("feedback"));
    ins.algorithm(set.getIntValue("algorithm"));
    ins.ams(set.getIntValue("ams"));
    ins.fms(set.getIntValue("fms"));
    ins.midi_velocity_offset = set.getIntValue("midi_velocity_offset");
    ins.percussion_key_number = set.getIntValue("percussion_key_number");

    for (unsigned opnum = 0; opnum < 4; ++opnum) {
        const String opfx = ((const char *[]){ "op1", "op3", "op2", "op4" })[opnum];
        ins.detune(opnum, set.getIntValue(opfx + "detune"));
        ins.fmul(opnum, set.getIntValue(opfx + "fmul"));
        ins.level(opnum, set.getIntValue(opfx + "level"));
        ins.ratescale(opnum, set.getIntValue(opfx + "ratescale"));
        ins.attack(opnum, set.getIntValue(opfx + "attack"));
        ins.am(opnum, set.getIntValue(opfx + "am"));
        ins.decay1(opnum, set.getIntValue(opfx + "decay1"));
        ins.decay2(opnum, set.getIntValue(opfx + "decay2"));
        ins.sustain(opnum, set.getIntValue(opfx + "sustain"));
        ins.release(opnum, set.getIntValue(opfx + "release"));
        ins.ssgenable(opnum, set.getIntValue(opfx + "ssgenable"));
        ins.ssgwave(opnum, set.getIntValue(opfx + "ssgwave"));
    }

    ins.delay_off_ms = set.getIntValue("delay_off_ms");
    ins.delay_on_ms = set.getIntValue("delay_on_ms");

    return ins;
}

PropertySet Instrument_Global_Parameters::to_properties() const
{
    PropertySet set;
    set.setValue("volume_model", (int)volume_model);
    set.setValue("lfo_enable", lfo_enable);
    set.setValue("lfo_frequency", (int)lfo_frequency);
    return set;
}

Instrument_Global_Parameters Instrument_Global_Parameters::from_properties(const PropertySet &set)
{
    Instrument_Global_Parameters gp;
    gp.volume_model = set.getIntValue("volume_model");
    gp.lfo_enable = set.getBoolValue("lfo_enable");
    gp.lfo_frequency = set.getIntValue("lfo_frequency");
    return gp;
}
