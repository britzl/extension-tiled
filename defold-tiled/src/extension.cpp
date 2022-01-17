
#include <dmsdk/sdk.h>
#include "script_tiled.h"

static dmExtension::Result AppInitializeTiled(dmExtension::AppParams* params)
{
    return dmExtension::RESULT_OK;
}

static dmExtension::Result InitializeTiled(dmExtension::Params* params)
{
    dmTiled::ScriptTiledRegister(params->m_L);
    dmLogInfo("Registered tiled extension");
    return dmExtension::RESULT_OK;
}

static dmExtension::Result AppFinalizeTiled(dmExtension::AppParams* params)
{
    return dmExtension::RESULT_OK;
}

static dmExtension::Result FinalizeTiled(dmExtension::Params* params)
{
    return dmExtension::RESULT_OK;
}

DM_DECLARE_EXTENSION(TiledExt, "TiledExt", AppInitializeTiled, AppFinalizeTiled, InitializeTiled, 0, 0, FinalizeTiled);
