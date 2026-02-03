# Let's look at gui...

# DrawContext
- Holds `GuiRenderState`

# GuiRenderstate
- Holds Root `Layer`s and current `Layer`
## Layer 
- Holds `up` variable pointing to next Layer
- Holds renderStates
    - simpleElement
    - preparedText (= simpleElement)
    - itemElement
    - textElement
    - specialElement

## RenderState
- x1,x2,y1,y2, pose, screenRect, comp4133, comp4274
- setupVertex gets all vertices

# GuiRenderer
1. prepare
    - specialElements
    - itemElements
    - textElements...
    - sortSimpleElements
    - prepareSimpleElements
2. renderPreparedDraws
3. cleanup

# RenderLayer

# RenderPipeline (Blaze3d) FINAL.
- Attributes
  - location
  - shader (vertex / fragment)
  - shadeUniformDefinision
  - samplers
  - uniforms
  - depthtestfunc
  - polygonmode
  - cull
  - colorLogic
  - blendFunc
  - writeColor / Alpha / Depth
  - vertexFormat / Drawmode

# RenderPass
- 1. Set pipeline, setup scissor and stuff, bind textures, setIndexbuffer and call drawIndexed.