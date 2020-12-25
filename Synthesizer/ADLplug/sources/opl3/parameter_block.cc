//          Copyright Jean Pierre Cimalando 2018.
// Distributed under the Boost Software License, Version 1.0.
//    (See accompanying file LICENSE or copy at
//          http://www.boost.org/LICENSE_1_0.txt)

#include "parameter_block.h"
#include "adl/chip_settings.h"
#include "adl/instrument.h"
#include "adl/player.h"
#include "utility/pak.h"
#include "resources.h"
#include <wopl/wopl_file.h>
#include <fmt/format.h>
#include <cassert>

RESOURCE(Res, opl3_banks_pak);

static WOPLFile_Ptr default_wopl()
{
    Pak_File_Reader pak;
    if (!pak.init_with_data((const uint8_t *)Res::opl3_banks_pak.data, Res::opl3_banks_pak.size))
        assert(false);
    std::string default_wopl = pak.extract(0);
    assert(default_wopl.size() != 0);

    WOPLFile_Ptr file(WOPL_LoadBankFromMem((void *)default_wopl.data(), default_wopl.size(), nullptr));
    if (!file)
        throw std::bad_alloc();

    return file;
}

static Instrument default_instrument(const WOPLFile &file)
{
    WOPLBank *bank = nullptr;
    for (unsigned i = 0, n = file.banks_count_melodic; i < n && !bank; ++i) {
        WOPLBank *cur = &file.banks_melodic[i];
        if (cur->bank_midi_lsb == 0 && cur->bank_midi_msb == 0)
            bank = cur;
    }
    assert(bank);
    return Instrument::from_wopl(bank->ins[0]);
}

static Chip_Settings default_chip_settings()
{
    Chip_Settings cs;
    cs.emulator = ::get_emulator_defaults().default_index;
    return cs;
}

void Parameter_Block::setup_parameters(AudioProcessorEx &p)
{
    Chip_Settings cs = default_chip_settings();

    typedef AudioParameterType Pt;
    typedef NormalisableRange<float> Rf;

    p_mastervol = add_automatable_parameter<Pt::Float>(p, 0, "mastervol", "Master volume", Rf{0.0f, 10.0f}, 1.0f, String());

    StringArray emu_choices = get_emulator_defaults().choices;
    for (unsigned i = 0, n = emu_choices.size(); i < n; ++i) {
        if (emu_choices[i].isEmpty())
            emu_choices.set(i, "<Reserved " + String(i) + ">");
    }
    p_emulator = add_parameter<Pt::Choice>(p, 'chip', "emulator", "Emulator", emu_choices, cs.emulator, String());
    p_nchip = add_parameter<Pt::Int>(p, 'chip', "nchip", "Chip count", 1, 100, cs.chip_count, String());
    p_n4op = add_parameter<Pt::Int>(p, 'chip', "n4op", "4op channel count", 0, 600, cs.fourop_count, String());

    WOPLFile_Ptr wopl = default_wopl();
    Instrument ins = default_instrument(*wopl);

    for (unsigned pn = 0; pn < 16; ++pn) {
        Part &part = this->part[pn];
        const uint32_t tag = ((uint8_t)'i' << 24) | ((uint8_t)'n' << 16) | ((uint8_t)'s' << 8) | pn;

        {
            String idprefix = fmt::format("P{:d}", pn + 1);
            String nameprefix = fmt::format("[Part {:d}] ", pn + 1);

            auto id = [idprefix](const char *x) -> String { return idprefix + x; };
            auto name = [nameprefix](const char *x) -> String { return nameprefix + x; };

            part.p_is4op = add_internal_parameter<Pt::Bool>(p, tag, id("is4op"), name("4op"), ins.four_op(), String());
            part.p_ps4op = add_internal_parameter<Pt::Bool>(p, tag, id("ps4op"), name("Pseudo 4op"), ins.pseudo_four_op(), String());
            part.p_blank = add_internal_parameter<Pt::Bool>(p, tag, id("blank"), name("Blank"), ins.blank(), String());
            StringArray con_choices = {"FM", "AM"};
            part.p_con12 = add_internal_parameter<Pt::Choice>(p, tag, id("con12"), name("Mode 1-2"), con_choices, ins.con12(), String());
            part.p_con34 = add_internal_parameter<Pt::Choice>(p, tag, id("con34"), name("Mode 3-4"), con_choices, ins.con34(), String());
            part.p_tune12 = add_internal_parameter<Pt::Int>(p, tag, id("tune12"), name("Note offset 1-2"), -127, +127, ins.note_offset1, String());
            part.p_tune34 = add_internal_parameter<Pt::Int>(p, tag, id("tune34"), name("Note offset 3-4"), -127, +127, ins.note_offset2, String());
            part.p_fb12 = add_internal_parameter<Pt::Int>(p, tag, id("fb12"), name("Feedback 1-2"), 0, 7, ins.fb12(), String());
            part.p_fb34 = add_internal_parameter<Pt::Int>(p, tag, id("fb34"), name("Feedback 3-4"), 0, 7, ins.fb34(), String());
            part.p_veloffset = add_internal_parameter<Pt::Int>(p, tag, id("veloffset"), name("Velocity offset"), -127, +127, ins.midi_velocity_offset, String());
            part.p_voice2ft = add_internal_parameter<Pt::Int>(p, tag, id("voice2ft"), name("Voice 2 fine tune"), -127, +127, ins.second_voice_detune, String());
            part.p_drumnote = add_internal_parameter<Pt::Int>(p, tag, id("drumnote"), name("Percussion note"), 0, 127, ins.percussion_key_number, String());
        }

        for (unsigned opnum = 0; opnum < 4; ++opnum) {
            String idprefix = fmt::format(
                "P{:d}{:s}", pn + 1,
                ((const char *[]){ "c1", "m1", "c2", "m2" })[opnum]);
            String nameprefix = fmt::format(
                "[Part {:d}] {:s} ", pn + 1,
                ((const char *[]){ "Carrier 1", "Modulator 1", "Carrier 2", "Modulator 2" })[opnum]);

            auto id = [idprefix](const char *x) -> String { return idprefix + String(x); };
            auto name = [nameprefix](const char *x) -> String { return nameprefix + String(x); };

            Operator &op = part.nth_operator(opnum);
            op.p_attack = add_internal_parameter<Pt::Int>(p, tag, id("attack"), name("Attack"), 0, 15, ins.attack(opnum), String());
            op.p_decay = add_internal_parameter<Pt::Int>(p, tag, id("decay"), name("Decay"), 0, 15, ins.decay(opnum), String());
            op.p_sustain = add_internal_parameter<Pt::Int>(p, tag, id("sustain"), name("Sustain"), 0, 15, ins.sustain(opnum), String());
            op.p_release = add_internal_parameter<Pt::Int>(p, tag, id("release"), name("Release"), 0, 15, ins.release(opnum), String());
            op.p_level = add_automatable_parameter<Pt::Int>(p, tag, id("level"), name("Level"), 0, 63, ins.level(opnum), String());
            op.p_ksl = add_internal_parameter<Pt::Int>(p, tag, id("ksl"), name("Key scale level"), 0, 3, ins.ksl(opnum), String());
            op.p_fmul = add_internal_parameter<Pt::Int>(p, tag, id("fmul"), name("Frequency multiplier"), 0, 15, ins.fmul(opnum), String());
            op.p_trem = add_internal_parameter<Pt::Bool>(p, tag, id("trem"), name("Tremolo"), ins.trem(opnum), String());
            op.p_vib = add_internal_parameter<Pt::Bool>(p, tag, id("vib"), name("Vibrato"), ins.vib(opnum), String());
            op.p_sus = add_internal_parameter<Pt::Bool>(p, tag, id("sus"), name("Sustaining"), ins.sus(opnum), String());
            op.p_env = add_internal_parameter<Pt::Bool>(p, tag, id("env"), name("Key scaling"), ins.env(opnum), String());
            StringArray waves {
                "Sine",
                "Half sine",
                "Absolute sine",
                "Pulse sine",
                "Alternating sine",
                "Camel sine",
                "Square",
                "Logarithmic sawtooth",
            };
            op.p_wave = add_internal_parameter<Pt::Choice>(p, tag, id("wave"), name("Waveform"), waves, ins.wave(opnum), String());
        }
    }

    StringArray volmodel_choices = {"Generic", "Native", "DMX", "Apogee", "Win9x"};
    p_volmodel = add_parameter<Pt::Choice>(p, 'glob', "volmodel", "Volume model", volmodel_choices, wopl->volume_model, String());
    p_deeptrem = add_parameter<Pt::Bool>(p, 'glob', "deeptrem", "Deep tremolo", wopl->opl_flags & WOPL_FLAG_DEEP_TREMOLO, String());
    p_deepvib = add_parameter<Pt::Bool>(p, 'glob', "deepvib", "Deep vibrato", wopl->opl_flags & WOPL_FLAG_DEEP_VIBRATO, String());
}

Chip_Settings Parameter_Block::chip_settings() const
{
    Chip_Settings cs;
    cs.emulator = p_emulator->getIndex();
    cs.chip_count = p_nchip->get();
    cs.fourop_count = p_n4op->get();
    return cs;
}

Instrument_Global_Parameters Parameter_Block::global_parameters() const
{
    Instrument_Global_Parameters gp;
    gp.volume_model = p_volmodel->getIndex();
    gp.deep_tremolo = p_deeptrem->get();
    gp.deep_vibrato = p_deepvib->get();
    return gp;
}

void Parameter_Block::set_chip_settings(const Chip_Settings &cs)
{
    *p_emulator = cs.emulator;
    *p_nchip = cs.chip_count;
    *p_n4op = cs.fourop_count;
}

void Parameter_Block::set_global_parameters(const Instrument_Global_Parameters &gp)
{
    *p_volmodel = gp.volume_model;
    *p_deeptrem = gp.deep_tremolo;
    *p_deepvib = gp.deep_vibrato;
}

Instrument Parameter_Block::Part::instrument() const
{
    Instrument ins;
    ins.version = Instrument::latest_version;
    ins.inst_flags = 0;

    ins.four_op(p_is4op->get());
    ins.pseudo_four_op(p_ps4op->get());
    ins.blank(p_blank->get());
    ins.con12(p_con12->getIndex());
    ins.con34(p_con34->getIndex());
    ins.note_offset1 = p_tune12->get();
    ins.note_offset2 = p_tune34->get();
    ins.fb12(p_fb12->get());
    ins.fb34(p_fb34->get());
    ins.midi_velocity_offset = p_veloffset->get();
    ins.second_voice_detune = p_voice2ft->get();
    ins.percussion_key_number = p_drumnote->get();

    for (unsigned opnum = 0; opnum < 4; ++opnum) {
        const Parameter_Block::Operator &op = nth_operator(opnum);
        ins.attack(opnum, op.p_attack->get());
        ins.decay(opnum, op.p_decay->get());
        ins.sustain(opnum, op.p_sustain->get());
        ins.release(opnum, op.p_release->get());
        ins.level(opnum, op.p_level->get());
        ins.ksl(opnum, op.p_ksl->get());
        ins.fmul(opnum, op.p_fmul->get());
        ins.trem(opnum, op.p_trem->get());
        ins.vib(opnum, op.p_vib->get());
        ins.sus(opnum, op.p_sus->get());
        ins.env(opnum, op.p_env->get());
        ins.wave(opnum, op.p_wave->getIndex());
    }

    return ins;
}

void Parameter_Block::Part::set_instrument(const Instrument &ins)
{
    *p_is4op = ins.four_op();
    *p_ps4op = ins.pseudo_four_op();
    *p_blank = ins.blank();
    *p_con12 = ins.con12();
    *p_con34 = ins.con34();
    *p_tune12 = ins.note_offset1;
    *p_tune34 = ins.note_offset2;
    *p_fb12 = ins.fb12();
    *p_fb34 = ins.fb34();
    *p_veloffset = ins.midi_velocity_offset;
    *p_voice2ft = ins.second_voice_detune;
    *p_drumnote = ins.percussion_key_number;

    for (unsigned opnum = 0; opnum < 4; ++opnum) {
        Parameter_Block::Operator &op = nth_operator(opnum);
        *op.p_attack = ins.attack(opnum);
        *op.p_decay = ins.decay(opnum);
        *op.p_sustain = ins.sustain(opnum);
        *op.p_release = ins.release(opnum);
        *op.p_level = ins.level(opnum);
        *op.p_ksl = ins.ksl(opnum);
        *op.p_fmul = ins.fmul(opnum);
        *op.p_trem = ins.trem(opnum);
        *op.p_vib = ins.vib(opnum);
        *op.p_sus = ins.sus(opnum);
        *op.p_env = ins.env(opnum);
        *op.p_wave = ins.wave(opnum);
    }
}
