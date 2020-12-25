//          Copyright Jean Pierre Cimalando 2018.
// Distributed under the Boost Software License, Version 1.0.
//    (See accompanying file LICENSE or copy at
//          http://www.boost.org/LICENSE_1_0.txt)

#pragma once
#include "JuceHeader.h"
#include "messages.h"
#include "adl/instrument.h"
#include "adl/chip_settings.h"
#include "ui/components/info_display.h"
#include "ui/utility/key_maps.h"
#include <vector>
#include <array>
#include <map>
#include <memory>
class AdlplugAudioProcessor;
struct Parameter_Block;
class Configuration;

template <class T>
class Generic_Main_Component :
    public Component, public FocusChangeListener,
    public TextEditor::Listener,
    public MidiKeyboardStateListener,
    public Info_Display {
public:
    T *self();
    const T *self() const;

    Generic_Main_Component(AdlplugAudioProcessor &proc, Parameter_Block &pb, Configuration &conf);
    virtual ~Generic_Main_Component();

    void setup_generic_components();

    void request_state_from_processor();

    bool is_percussion_channel(unsigned channel) const;
    void send_rename_bank(Bank_Id bank, const String &name);
    void send_rename_program(Bank_Id bank, unsigned pgm, const String &name);
    void send_create_program(Bank_Id bank, unsigned pgm);

    Instrument *find_instrument(uint32_t program, Instrument *if_not_found);
    void reload_selected_instrument(NotificationType ntf);
    void send_selection_update();
    void receive_bank_slots(const Messages::Fx::NotifyBankSlots &msg);
    void receive_global_parameters(const Instrument_Global_Parameters &gp);
    void receive_instrument(Bank_Id bank, unsigned pgm, const Instrument &ins);
    void receive_chip_settings(const Chip_Settings &cs);
    void receive_selection(unsigned part, Bank_Id bank, uint8_t pgm);
    void update_instrument_choices();
    void set_program_selection(int selection, NotificationType ntf);
    static String program_selection_to_string(int selection);

    void handle_selected_program(int selection);
    void handle_edit_program();
    void handle_add_program();

    void create_image_overlay(Component &component, Image image, float ratio);

    void vu_update();
    void cpu_load_update();
    void midi_activity_update();
    void midi_keys_update();
    void parameters_update();

    void update_master_volume_label();

    void update_emulator_icon();
    void build_emulator_menu(PopupMenu &menu);
    int select_emulator_by_menu();

    void handle_load_bank(Component *clicked);
    void handle_save_bank(Component *clicked);
    void load_bank(const File &file, int format);
    void load_single_instrument(uint32_t program, const File &file, int format);
    void load_bank_mem(const uint8_t *mem, size_t length, const String &bank_name, int format);
    void load_single_instrument_mem(uint32_t program, const uint8_t *mem, size_t length, const String &bank_name, int format);
    void save_bank(const File &file);
    void save_single_instrument(uint32_t program, const File &file);

    void handle_change_keymap();
    void handle_change_octave(int diff);

    void set_int_parameter_with_delay(unsigned delay, AudioParameterInt &p, int v);

    double get_volume_knob_value() const;
    void set_volume_knob_value(double value, NotificationType ntf);

    void initialize_bank_directory();
    void change_bank_directory(const File &directory);

    void on_change_bank_title(const String &title, NotificationType ntf);

    void textEditorTextChanged(TextEditor &editor) override;

    void handleNoteOn(MidiKeyboardState *, int channel, int note, float velocity) override;
    void handleNoteOff(MidiKeyboardState *, int channel, int note, float velocity) override;

    void focusGained(FocusChangeType cause) override;
    void globalFocusChanged(Component *component) override;

private:
    void display_info_now(const String &text) override;

private:
    void get_master_volume_limits(
        const AudioParameterFloat &parameter,
        double &linmin, double &linmax, double &dbmin, double &dbmax) const;

protected:
    bool write_to_processor(
        User_Message tag, const void *msgbody, unsigned msgsize);

    AdlplugAudioProcessor *proc_ = nullptr;
    Parameter_Block *parameter_block_ = nullptr;
    Configuration *conf_ = nullptr;

    unsigned midichannel_ = 0;
    uint32_t midiprogram_[16] = {};

    struct Editor_Bank {
        char melodic_name[32] = {};
        char percussion_name[32] = {};
        PopupMenu ins_menu;
        std::array<Instrument, 256> ins;
    };
    std::map<uint32_t, Editor_Bank> instrument_map_;
    Instrument_Global_Parameters instrument_gparam_;
    Chip_Settings chip_settings_;

    std::map<String, std::unique_ptr<Timer>> parameters_delayed_;

    File bank_directory_;

    MidiKeyboardState midi_kb_state_;
    int midi_kb_octave_ = 6;
    Key_Layout last_key_layout_ = Key_Layout::Default;

    std::unique_ptr<Timer> vu_timer_;
    std::unique_ptr<Timer> cpu_load_timer_;
    std::unique_ptr<Timer> midi_activity_timer_;
    std::unique_ptr<Timer> midi_keys_timer_;
    std::unique_ptr<Timer> parameter_watch_timer_;

    std::vector<std::unique_ptr<ImageComponent>> image_overlays_;

    Component::SafePointer<DialogWindow> dlg_new_program_;
    Component::SafePointer<DialogWindow> dlg_edit_program_;
    Component::SafePointer<DialogWindow> dlg_about_;

    class Mouse_Hover_Listener : public MouseListener {
    public:
        explicit Mouse_Hover_Listener(T *component);
        void mouseEnter(const MouseEvent &event) override;
    private:
        T *component_ = nullptr;
    };
    std::unique_ptr<Mouse_Hover_Listener> mouse_hover_listener_;
};
