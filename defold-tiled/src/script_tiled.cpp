#include <dmsdk/sdk.h>

namespace dmTiled
{
    static int Tiled_Hello(lua_State* L)
    {
        DM_LUA_STACK_CHECK(L, 0);
        dmLogInfo("Hello");
        return 0;
    }

    static const luaL_reg TILED_FUNCTIONS[] =
    {
            {"hello",    Tiled_Hello},
            {0, 0}
    };

    void ScriptTiledRegister(lua_State* L)
    {
        luaL_register(L, "tiled", TILED_FUNCTIONS);
        lua_pop(L, 1);
    }
}
