/*
  ==============================================================================

  This is an automatically generated GUI class created by the Projucer!

  Be careful when adding custom code to these files, as only the code within
  the "//[xyz]" and "//[/xyz]" sections will be retained when the file is loaded
  and re-saved.

  Created with Projucer version: 5.4.1

  ------------------------------------------------------------------------------

  The Projucer is part of the JUCE library.
  Copyright (c) 2017 - ROLI Ltd.

  ==============================================================================
*/

#pragma once

//[Headers]     -- You can add your own extra header files here --
#include "JuceHeader.h"
#include "components/opl3_waves.h"
#include "ui/components/styled_knobs.h"
class Wave_Label;
class Info_Display;
struct Instrument;
struct Parameter_Block;
//[/Headers]



//==============================================================================
/**
                                                                    //[Comments]
    An auto-generated component, created by the Projucer.

    Describe your class and how it works here!
                                                                    //[/Comments]
*/
class Operator_Editor  : public Component,
                         public Knob::Listener,
                         public Button::Listener
{
public:
    //==============================================================================
    Operator_Editor (unsigned op_id, Parameter_Block &pb);
    ~Operator_Editor();

    //==============================================================================
    //[UserMethods]     -- You can add your own custom methods in this section.
    void set_operator_parameters(const Instrument &ins, unsigned op, NotificationType ntf);

    void set_operator_enabled(bool b);

    void set_midi_channel(unsigned c)
        { midichannel_ = c; }

    void knob_value_changed(Knob *k) override;
    void knob_drag_started(Knob *k) override;
    void knob_drag_ended(Knob *k) override;

    void paintOverChildren(Graphics &g) override;

    void set_info_display(Info_Display *info)
        { info_ = info; }
    bool display_info_for_component(Component *c);
    //[/UserMethods]

    void paint (Graphics& g) override;
    void resized() override;
    void buttonClicked (Button* buttonThatWasClicked) override;



private:
    //[UserVariables]   -- You can add your own custom variables in this section.
    unsigned operator_id_ = 0;
    bool operator_enabled_ = true;
    Parameter_Block *parameter_block_ = nullptr;
    Info_Display *info_ = nullptr;
    unsigned midichannel_ = 0;
    OPL3_Waves chip_waves_;
    //[/UserVariables]

    //==============================================================================
    std::unique_ptr<Styled_Knob_Default> kn_attack;
    std::unique_ptr<Styled_Knob_Default> kn_decay;
    std::unique_ptr<Styled_Knob_Default> kn_sustain;
    std::unique_ptr<Styled_Knob_Default> kn_release;
    std::unique_ptr<TextButton> btn_prev_wave;
    std::unique_ptr<TextButton> btn_next_wave;
    std::unique_ptr<TextButton> btn_trem;
    std::unique_ptr<TextButton> btn_vib;
    std::unique_ptr<TextButton> btn_sus;
    std::unique_ptr<TextButton> btn_env;
    std::unique_ptr<Label> lbl_level;
    std::unique_ptr<Wave_Label> lbl_wave;
    std::unique_ptr<Label> label;
    std::unique_ptr<Label> label2;
    std::unique_ptr<Label> label3;
    std::unique_ptr<Label> label4;
    std::unique_ptr<Label> label5;
    std::unique_ptr<Label> label6;
    std::unique_ptr<Label> label7;
    std::unique_ptr<Label> label8;
    std::unique_ptr<Label> lbl_fmul;
    std::unique_ptr<Label> lbl_ksl;
    std::unique_ptr<Styled_Slider_DefaultSmall> sl_level;
    std::unique_ptr<Styled_Slider_DefaultSmall> sl_fmul;
    std::unique_ptr<Styled_Slider_DefaultSmall> sl_ksl;


    //==============================================================================
    JUCE_DECLARE_NON_COPYABLE_WITH_LEAK_DETECTOR (Operator_Editor)
};

//[EndFile] You can add extra defines here...
//[/EndFile]
